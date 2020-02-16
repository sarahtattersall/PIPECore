package uk.ac.imperial.pipe.models.petrinet;

public class ResultEntry<V> {
    String message;
    V value;

    public ResultEntry(String message, V value) {
        this.message = message;
        this.value = value;
    }
}
