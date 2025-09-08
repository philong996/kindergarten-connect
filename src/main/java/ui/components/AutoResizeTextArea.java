package ui.components;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class AutoResizeTextArea extends JTextArea {

    private int maxRows; // giới hạn tối đa số dòng, dùng 0 nếu không giới hạn

    public AutoResizeTextArea(int maxRows) {
        super();
        this.maxRows = maxRows;
        setLineWrap(true);
        setWrapStyleWord(true);
        setRows(1); // bắt đầu với 1 dòng

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateSize(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateSize(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateSize(); }
        });
    }

    public AutoResizeTextArea(String text, int maxRows) {
        this(maxRows); // gọi constructor cũ để thiết lập line wrap, listener
        setText(text);
        updateSize(); // cập nhật chiều cao ngay sau khi set text
    }

    private void updateSize() {
        int lines = getLineCount();
        if (maxRows > 0) {
            lines = Math.min(lines, maxRows);
        }
        setRows(lines);
        revalidate(); // refresh layout để panel tự điều chỉnh
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        pref.width = Math.max(pref.width, 200); // độ rộng tối thiểu nếu muốn
        return pref;
    }
}
