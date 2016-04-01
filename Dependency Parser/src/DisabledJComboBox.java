import java.awt.Component;
import java.util.HashSet;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

public class DisabledJComboBox<E> extends JComboBox<E> {
	private static final long serialVersionUID = 2221131204170507404L;

	private HashSet<Integer> disabledIndex;
	
	public DisabledJComboBox(E[] items) {
		super(items);
		ListCellRenderer<? super E> lcr = super.getRenderer();
		
		super.setRenderer(new DisabledComboBoxRenderer(lcr));
		disabledIndex = new HashSet<Integer>();
	}
	
	public void setIndexEnabled(int index, boolean enabled) {
		if(enabled)
			disabledIndex.remove(index);
		else
			disabledIndex.add(index);
	}
	
	@Override
	public void setSelectedIndex(int index) {
		if (!disabledIndex.contains(index)) {
			super.setSelectedIndex(index);
		}
	}
	

	class DisabledComboBoxRenderer implements ListCellRenderer<E> {
		private ListCellRenderer<? super E> lcr;
		public DisabledComboBoxRenderer(ListCellRenderer<? super E> lcr) {
			this.lcr=lcr;
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Component getListCellRendererComponent(JList list, E value, int index, boolean isSelected,
				boolean cellHasFocus) {

			Component c = lcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (disabledIndex.contains(index)) {
				c.setBackground(list.getBackground());
				c.setForeground(UIManager.getColor("Label.disabledForeground"));
			}

			return c;
		}
	}
}