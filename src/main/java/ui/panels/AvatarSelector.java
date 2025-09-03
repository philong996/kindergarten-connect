package ui.panels;

import javax.swing.*;

import model.Student;
import ui.components.AppColor;
import util.ProfileImageUtil;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class AvatarSelector extends JPanel {
    private JLabel avatarLabel;
    private JLabel nameLabel;

    private List<Student> students;
    private int currentIndex = 0;
    private Consumer<Student> onAvatarChange;

    public AvatarSelector(List<Student> children) {
        this.students = children;

        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5)); // canh trái
        setOpaque(false);

        // Avatar + tên mặc định (student đầu tiên)
        if (!students.isEmpty()) {
            Student first = students.get(currentIndex);
            byte[] imageProfileData = first.getProfileImage();
            ImageIcon icon = ProfileImageUtil.loadProfileImageFromBytes(imageProfileData, 80, 80);
            avatarLabel = new JLabel(icon);
            System.out.println("name: " + first.getName());
            nameLabel = new JLabel(
            "<html>"
            + "<div style='width:500px;'>"  // đặt độ rộng tối đa
            + "<span style='font-size:16pt;'><b>" + first.getName().toUpperCase() + "</b></span>"
            + " - Age: " + first.getAge()
            + "<br/><b>I'm enrolled at Kindergarten BÉ NGOAN (HCM) attending class " + first.getClassName() + "</b>"
            + "</div>"
            + "</html>");
        } else {
            avatarLabel = new JLabel();
            nameLabel = new JLabel("No student");
        }

        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // nameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel avatarPanel = new JPanel(new BorderLayout(5, 0));
        avatarPanel.setOpaque(false);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); 
        avatarPanel.add(avatarLabel, BorderLayout.WEST);
        avatarPanel.add(nameLabel, BorderLayout.CENTER);

        add(avatarPanel);

        // Click avatar -> hiển thị menu chọn student khác
        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!students.isEmpty()) {
                    showAvatarSelection();
                }
            }
        });
    }

    private void showAvatarSelection() {
        JPopupMenu popup = new JPopupMenu();

        for (int i = 0; i < students.size(); i++) {
            final int index = i;
            Student s = students.get(i);
            byte[] imageProfileData = s.getProfileImage();
            ImageIcon icon = ProfileImageUtil.loadProfileImageFromBytes(imageProfileData, 30, 30);
            JMenuItem item = new JMenuItem(s.getName(), icon);
            item.addActionListener(e -> setAvatar(index));
            item.setBackground(AppColor.getColor("lightGraylishYellow"));
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
            popup.add(item);
        }
        popup.setBackground(AppColor.getColor("lightGraylishYellow"));
        popup.show(avatarLabel, 10, avatarLabel.getHeight());
    }

    private void setAvatar(int index) {
        currentIndex = index;
        Student s = students.get(index);
        ImageIcon icon = ProfileImageUtil.loadProfileImageFromBytes(s.getProfileImage(), 80, 80);
        avatarLabel.setIcon(icon);
        System.out.println("Selected student: " + s.getName());
        String newText = "<html>"
            + "<div style='width:500px;'>"  // đặt độ rộng tối đa
            + "<span style='font-size:16pt;'><b>" + s.getName().toUpperCase() + "</b></span>"
            + " - Age: " + s.getAge()
            + "<br/><b>I'm enrolled at Kindergarten BÉ NGOAN (HCM) attending class " + s.getClassName() + "</b>"
            + "</div>"
            + "</html>";

        nameLabel.setText(newText);
        if (onAvatarChange != null) {
            onAvatarChange.accept(s);
        }
    }

    public Student getSelectedStudent() {
        return students.isEmpty() ? null : students.get(currentIndex);
    }

    public void setOnAvatarChange(Consumer<Student> listener) {
        this.onAvatarChange = listener;
    }
}
