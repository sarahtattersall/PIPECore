package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;

public class ListWrapper<T> {

    private List<T> items;

    public ListWrapper() {
        items = new ArrayList<T>();
    }

    public ListWrapper(List<T> items) {
        this.items = items;
    }

    @XmlAnyElement(lax = true)
    public List<T> getItems() {
        return items;
    }

}
