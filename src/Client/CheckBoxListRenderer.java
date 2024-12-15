package Client;

import javax.swing.*;
import java.awt.*;

public class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer<String> {
    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        setText(value);
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

        CheckBoxListModel model = (CheckBoxListModel) list.getModel();
        setSelected(model.isChecked(index)); // 선택 상태 반영
        return this;
    }
}