package uk.ac.imperial.pipe.models.petrinet;

public class TestingContext {
    public int num;
    public String content;
    private String placeId;
    private boolean mark;

    public TestingContext(int num) {
        this(num, "P1", true);
    }

    public TestingContext(int num, String placeId, boolean mark) {
        this.num = num;
        this.placeId = placeId;
        this.mark = mark;
    }

    public String getUpdatedContext() {
        return content + num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public final String getPlaceId() {
        return placeId;
    }

    public final boolean isMark() {
        return mark;
    }

}
