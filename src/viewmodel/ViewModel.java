package viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.Model;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ViewModel extends Observable implements Observer {

    private Model model;

    public StringProperty ip;
    public StringProperty port;
    public StringProperty airplanePosition;
    public StringProperty autopilotScript;

    public ViewModel(Model model) {
        this.model = model;

        ip = new SimpleStringProperty();
        port = new SimpleStringProperty();
        airplanePosition = new SimpleStringProperty();
        autopilotScript = new SimpleStringProperty();
    }

    public void connectToFlightGear() {
        model.connect(ip.get(), Integer.parseInt(port.get()));
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::updateAirplanePosition, 0, 250, TimeUnit.MILLISECONDS);
        setChanged();
        notifyObservers("Connected");
    }

    public void runAutopilotScript() {
        model.runAutopilotScript(autopilotScript.get().split("\n"));
    }

    private void updateAirplanePosition() {
        CompletableFuture
                .supplyAsync(() -> model.getAirplanePosition())
                .thenApply(position -> position[0] + "," + position[1])
                .thenAccept(position -> airplanePosition.set(position));
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}