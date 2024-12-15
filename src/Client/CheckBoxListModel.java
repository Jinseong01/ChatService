package Client;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CheckBoxListModel extends AbstractListModel<String> {
    private final List<String> items; // 항목 리스트
    private final List<Boolean> checked; // 선택 상태 리스트

    public CheckBoxListModel(List<String> items) {
        this.items = new ArrayList<>(items);
        this.checked = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            checked.add(false); // 초기 상태: 모두 선택되지 않음
        }
    }

    public boolean isChecked(int index) {
        return checked.get(index);
    }

    public void setChecked(int index, boolean value) {
        checked.set(index, value);
        fireContentsChanged(this, index, index);
    }

    public List<String> getCheckedItems() {
        List<String> selectedItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (checked.get(i)) {
                selectedItems.add(items.get(i));
            }
        }
        return selectedItems;
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public String getElementAt(int index) {
        return items.get(index);
    }
}