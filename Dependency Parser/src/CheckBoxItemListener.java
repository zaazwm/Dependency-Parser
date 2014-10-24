import java.awt.Checkbox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


public class CheckBoxItemListener implements ItemListener{

	@Override
	public void itemStateChanged(ItemEvent e) {
		Checkbox cb=(Checkbox)e.getItemSelectable();
		if(cb.getState()==true) {
			//ApplicationControl.predictArcTag=true;
			ApplicationControl.newPredArcTag=true;
		}
		else {
			//ApplicationControl.predictArcTag=false;
			ApplicationControl.newPredArcTag=false;
		}
		//System.out.println("CheckBox: "+cb.getState());
	}
}
