package ui.components;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
    public MultiLineCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        setText(value == null ? "" : value.toString());
        setFont(table.getFont());

        // Màu khi select/không select
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }

        // Phải set width trước để tính đúng preferredHeight cho wrap
        int colWidth = table.getColumnModel().getColumn(column).getWidth()
                - table.getIntercellSpacing().width;
        if (colWidth > 0) setSize(colWidth, Short.MAX_VALUE);

        int preferred = getPreferredSize().height;

        // Đảm bảo hàng đủ cao cho nội dung dài (chỉ tăng chiều cao)
        if (table.getRowHeight(row) < preferred) {
            table.setRowHeight(row, preferred);
        }

        return this;
    }
}
