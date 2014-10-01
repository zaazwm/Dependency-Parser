import java.awt.Checkbox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


public class CheckBoxItemListener implements ItemListener{

	@Override
	public void itemStateChanged(ItemEvent e) {
		Checkbox cb=(Checkbox)e.getItemSelectable();
		if(cb.getState()==true) {
			ApplicationControl.predictArcTag=true;
		}
		else {
			ApplicationControl.predictArcTag=false;
		}
		//System.out.println("CheckBox: "+cb.getState());
	}
}
