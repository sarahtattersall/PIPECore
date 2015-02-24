package uk.ac.imperial.pipe.models.petrinet;

public class TestingContext {
	public int num;
	public String content; 
	public TestingContext(int num) {
		this.num = num; 
	}
	public String getUpdatedContext() {
		return content+num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}
