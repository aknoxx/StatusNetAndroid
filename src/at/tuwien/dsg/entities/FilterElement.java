package at.tuwien.dsg.entities;

public class FilterElement {

	private CharSequence name;
	private boolean selected;
	
	public FilterElement(CharSequence name, boolean selected) {
		super();
		this.name = name;
		this.selected = selected;
	}

	public CharSequence getName() {
		return name;
	}

	public void setName(CharSequence name) {
		this.name = name;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
