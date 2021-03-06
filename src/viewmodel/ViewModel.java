package viewmodel;

import javafx.beans.property.*;
import model.Model;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ViewModel implements Observer {

    private final Model model;

    private boolean connectedToPathfinder;

    public StringProperty ip = new SimpleStringProperty();
    public StringProperty port = new SimpleStringProperty();
    public StringProperty autopilotScript = new SimpleStringProperty();

    public DoubleProperty rudder = new SimpleDoubleProperty();
    public DoubleProperty throttle = new SimpleDoubleProperty();
    public DoubleProperty aileron = new SimpleDoubleProperty();
    public DoubleProperty elevator = new SimpleDoubleProperty();

    public ListProperty<String> grid = new SimpleListProperty<>();
    public StringProperty airplanePosition = new SimpleStringProperty();
    public StringProperty destinationPosition = new SimpleStringProperty();
    public StringProperty path = new SimpleStringProperty();

    public ViewModel(Model model) {
        this.model = model;
    }

    public void connectToFlightGear() {
        if (ip.get() == null || port.get() == null) return;
        model.connectToFlightGear(ip.get(), Integer.parseInt(port.get()));
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::updateAirplanePosition, 0, 250, TimeUnit.MILLISECONDS);
    }

    public void connectToPathfinder() {
        if (ip.get() == null || port.get() == null) return;
        model.connectToPathfinder(ip.get(), Integer.parseInt(port.get()));
    }

    public void calculatePath() {
        if (!connectedToPathfinder || grid == null || airplanePosition == null || destinationPosition == null) return;
        model.calculatePath(grid.get(), airplanePosition.get(), destinationPosition.get());
    }

    public void runAutopilotScript() {
        if (autopilotScript.get() == null) return;
        model.runAutopilotScript(autopilotScript.get().split("\n"));
    }

    private void updateAirplanePosition() {
        CompletableFuture
                .supplyAsync(model::getAirplanePosition)
                .thenApply(position -> position[0] + "," + position[1])
                .thenAccept(position -> airplanePosition.set(position));
        rudder.set(model.getProperty("rudder"));
        throttle.set(model.getProperty("throttle"));
        aileron.set(model.getProperty("aileron"));
        elevator.set(model.getProperty("elevator"));
    }

    public void setMapCoordinatesAndScale(List<String> data) {
        double[] coordinates = Arrays
                .stream(data.get(0).split(","))
                .mapToDouble(Double::parseDouble)
                .toArray();
        double scale = Double.parseDouble(data.get(1).split(",")[0]);
        model.setMapCoordinatesAndScale(coordinates, scale);
    }

    public void setRudder() {
        model.setProperty("rudder", rudder.get());
    }

    public void setThrottle() {
        model.setProperty("throttle", throttle.get());
    }

    public void setAileronAndElevator() {
        model.setProperty("aileron", aileron.get());
        model.setProperty("elevator", elevator.get());
    }

    public void switchFlyingSystems() {
        model.switchFlyingSystems();
    }

    @Override
    public void update(Observable o, Object arg) {
        Model.Message message = (Model.Message) arg;
        switch (message) {
            case CONNECT_PATHFINDER_SUCCESS:
                connectedToPathfinder = true;
                break;
            case CONNECT_PATHFINDER_FAILURE:
                System.out.println("Could not connect to Pathfinding server.");
                break;
            case CALCULATE_PATH_SUCCESS:
                path.set(model.getPath());
                break;
            case CALCULATE_PATH_FAILURE:
                System.out.println("Could not find shortest path.");
                break;
            default: break;
        }
    }
}
