package StringReplacer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

import static javax.swing.GroupLayout.Alignment.*;

public class App extends JFrame implements ActionListener {
    private final Config config;
    private final JButton jButtonSave = new JButton("Save config");
    private final JButton jButtonChoose = new JButton("Choose folder");
    private final JButton jButtonStart = new JButton("Start!");
    private final JPanel jPanel = new JPanel();
    private final JTextField[] textFields = new JTextField[3];
    private final JLabel[] labels = new JLabel[3];
    private final JMenuItem[] items = new JMenuItem[3];
    private final JMenu mTheme = new JMenu("Theme");
    private final JMenu mHelp = new JMenu("Help");

    public App() throws IOException {
        config = Config.readFromJson();
        setup_UI();
        if (config.getDarkTheme())
            changeToDark();
        else
            changeToWhite();
        if (!config.getPathToFolder().equals(""))
            textFields[0].setText(config.getPathToFolder());
        if (!config.getReplaceString().equals(""))
            textFields[1].setText(config.getReplaceString());
        if (!config.getReplaceToString().equals(""))
            textFields[2].setText(config.getReplaceToString());
        this.setTitle("String replacer");
        this.setMinimumSize(new Dimension(500, 175));
        URL iconURL = getClass().getResource("/images/favicon.png");
        assert iconURL != null; // iconURL is null when not found
        ImageIcon icon = new ImageIcon(iconURL);
        this.setIconImage(icon.getImage());
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    private void setup_UI() {
        String[] themeArray = {"White", "Dark", "About"};
        for (int i = 0; i < items.length; i++) {
            items[i] = new JMenuItem(themeArray[i]);
            items[i].addActionListener(this);
            if (i != 2)
                mTheme.add(items[i]);
        }
        mHelp.add(items[2]);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(mTheme);
        menuBar.add(mHelp);
        jButtonSave.addActionListener(this);
        jButtonChoose.addActionListener(this);
        jButtonStart.addActionListener(this);
        String[] textAreaArray = {"Path to folder", "Finding string", "Replace string"};
        String[] labelArray = {"Folder to search in", "Find what:", "Replace with:"};
        for (int i = 0; i < textFields.length; i++) {
            labels[i] = new JLabel(labelArray[i]);
            textFields[i] = new JTextField(textAreaArray[i]);
            textFields[i].setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
//            textFields[i].setBorder(new EtchedBorder());
//            textFields[i].setMargin(new Insets(10,10,10,10));
//            textFields[i].setBorder(new LineBorder(new Color(0,0,0)));
        }

        this.setLayout(new BorderLayout());
        this.add(menuBar, BorderLayout.PAGE_START);
        this.add(jPanel, BorderLayout.CENTER);

        GroupLayout groupLayout = new GroupLayout(jPanel);
        jPanel.setLayout(groupLayout);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);

        groupLayout.setHorizontalGroup(
                groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup(TRAILING)
                                .addComponent(labels[0])
                                .addComponent(labels[1])
                                .addComponent(labels[2]))
                        .addGroup(groupLayout.createParallelGroup(LEADING)
                                .addComponent(textFields[0])
                                .addComponent(textFields[1])
                                .addComponent(textFields[2]))
                        .addGroup(groupLayout.createParallelGroup(LEADING)
                                .addComponent(jButtonChoose)
                                .addComponent(jButtonSave)
                                .addComponent(jButtonStart))
        );

        groupLayout.linkSize(SwingConstants.HORIZONTAL, jButtonChoose, jButtonSave, jButtonStart);

        groupLayout.setVerticalGroup(
                groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup(BASELINE)
                                .addComponent(labels[0])
                                .addComponent(textFields[0])
                                .addComponent(jButtonChoose))
                        .addGroup(groupLayout.createParallelGroup(LEADING)
                                .addGroup(groupLayout.createSequentialGroup()
                                        .addGroup(groupLayout.createParallelGroup(BASELINE)
                                                .addComponent(labels[1])
                                                .addComponent(textFields[1]))
                                        .addGroup(groupLayout.createParallelGroup(BASELINE)
                                                .addComponent(labels[2])
                                                .addComponent(textFields[2])))
                                .addGroup(groupLayout.createSequentialGroup()
                                        .addComponent(jButtonSave)
                                        .addComponent(jButtonStart)))
        );

        groupLayout.linkSize(SwingConstants.VERTICAL, textFields[0], jButtonChoose);
        groupLayout.linkSize(SwingConstants.VERTICAL, textFields[1], jButtonSave);
        groupLayout.linkSize(SwingConstants.VERTICAL, textFields[2], jButtonStart);
    }

    private void replaceStringRecursively() throws IOException {
        List<Path> files = Files.find(Paths.get(config.getPathToFolder()), 999,
                (p, bfa) -> bfa.isRegularFile()).collect(Collectors.toList());
        for (Path file : files) {
            try (FileReader fileReader = new FileReader(file.toFile())) {
                int i;
                StringBuilder stringBuilder = new StringBuilder();
                while ((i = fileReader.read()) != -1)
                    stringBuilder.append((char) i);
                String str = stringBuilder.toString().replace(config.getReplaceString(), config.getReplaceToString());
                try (FileWriter fileWriter = new FileWriter(file.toFile())) {
                    fileWriter.append(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }

        new App();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jButtonChoose) {
            JFileChooser jFileChooser = new JFileChooser();
            File file = new File(config.getPathToFolder());
            if (!file.exists())
                file = new File(".");
            jFileChooser.setCurrentDirectory(file);
            jFileChooser.setDialogTitle("Choose js override folder...");
            jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jFileChooser.showDialog(null, "Select") == JFileChooser.APPROVE_OPTION) {
                textFields[0].setText(jFileChooser.getSelectedFile().getAbsolutePath());
            } else {
                System.out.println("No Selection");
            }
        } else if (e.getSource() == jButtonSave) {
            config.setPathToFolder(textFields[0].getText().trim());
            config.setReplaceString(textFields[1].getText().trim());
            config.setReplaceToString(textFields[2].getText().trim());
            try {
                Config.saveToJson(config);
            } catch (IOException ignored) {
            }
        } else if (e.getSource() == jButtonStart) {
            config.setPathToFolder(textFields[0].getText().trim());
            config.setReplaceString(textFields[1].getText().trim());
            config.setReplaceToString(textFields[2].getText().trim());
            try {
                replaceStringRecursively();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            JFrame jFrame = new JFrame("Done");
            jFrame.setLayout(new FlowLayout());
            jFrame.add(new Label("Files processed!"));
            jFrame.setMinimumSize(new Dimension(250, 90));
            jFrame.setLocationRelativeTo(this);
            jFrame.setResizable(false);
            jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            jFrame.setVisible(true);
        } else if (e.getSource() == items[0] || e.getSource() == items[1]) {
            if (e.getSource() == items[0]) {
                changeToWhite();
                config.setDarkTheme(Boolean.FALSE);
                try {
                    Config.saveToJson(config);
                } catch (IOException ignored) {
                }
            } else if (e.getSource() == items[1]) {
                changeToDark();
                config.setDarkTheme(Boolean.TRUE);
                try {
                    Config.saveToJson(config);
                } catch (IOException ignored) {
                }
            }
            SwingUtilities.updateComponentTreeUI(this);
        } else if (e.getSource() == items[2]) {
            JFrame jFrame2 = new JFrame("About");
            jFrame2.setLayout(new FlowLayout());
            URL iconURL = getClass().getResource("/images/favicon.png");
            assert iconURL != null; // iconURL is null when not found
            JLabel pic = new JLabel(new ImageIcon(iconURL));
            jFrame2.add(pic);
            jFrame2.add(new Label("String Replacer v0.0.1"));
            jFrame2.setMinimumSize(new Dimension(250, 90));
            jFrame2.setLocationRelativeTo(this);
            jFrame2.setResizable(false);
            jFrame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            jFrame2.setVisible(true);
        }
    }

    private void changeToWhite() {
        try {
//            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.put("control", new Color(214, 217, 223));
            UIManager.put("Button.background", new Color(214, 217, 223));
//            UIManager.put("Button.foreground", new Color(0, 0, 0));
            UIManager.put("info", new Color(242, 242, 189));
//            UIManager.put("menu", new Color(237,239,242));
//            UIManager.put("Menu[Enabled].textForeground", new Color(35,35,36));
            UIManager.put("nimbusBase", new Color(51, 98, 140));
            UIManager.put("FileChooser.background", new Color(214, 217, 223));
            UIManager.put("nimbusAlertYellow", new Color(255, 220, 35));
            UIManager.put("nimbusDisabledText", new Color(142, 143, 145));
            UIManager.put("nimbusFocus", new Color(115, 164, 209));
            UIManager.put("nimbusGreen", new Color(176, 179, 50));
            UIManager.put("nimbusInfoBlue", new Color(47, 92, 180));
            UIManager.put("nimbusLightBackground", new Color(255, 255, 255));
            UIManager.put("nimbusOrange", new Color(191, 98, 4));
            UIManager.put("nimbusRed", new Color(169, 46, 34));
            UIManager.put("nimbusSelection", new Color(57, 105, 138));
            UIManager.put("scrollbar", new Color(205, 208, 213));
            UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
            UIManager.put("nimbusSelectionBackground", new Color(57, 105, 138));
            UIManager.put("text", new Color(0, 0, 0));
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            jButtonStart.setBackground(new Color(214, 217, 223));
            jButtonChoose.setBackground(new Color(214, 217, 223));
            jButtonSave.setBackground(new Color(214, 217, 223));
            mTheme.setForeground(new Color(35, 35, 36));
            mHelp.setForeground(new Color(35, 35, 36));
            for (JMenuItem item : items) {
                item.setForeground(new Color(35, 35, 36));
            }
        } catch (Exception e) {
            System.err.println("Exception caught:" + e);
        }
    }

    private void changeToDark() {
        try {
//            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.put("control", new Color(65, 68, 77));
//            UIManager.put("control", new Color(54, 57, 63));
            UIManager.put("Button.background", new Color(27, 28, 31, 255));
//            UIManager.put("Button.foreground", new Color(59, 68, 75));
            UIManager.put("info", new Color(54, 57, 63));
//            UIManager.put("menu", new Color(36, 38, 42));
//            UIManager.put("Menu[Enabled].textForeground", new Color(255, 255, 255));
            UIManager.put("nimbusBase", new Color(10, 10, 10));
            UIManager.put("FileChooser.background", new Color(54, 57, 63));
            UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
            UIManager.put("nimbusDisabledText", new Color(54, 57, 63));
            UIManager.put("nimbusFocus", new Color(18, 126, 170));
            UIManager.put("nimbusGreen", new Color(176, 179, 50));
            UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
//            UIManager.put("nimbusLightBackground", new Color(54, 57, 63));
            UIManager.put("nimbusLightBackground", new Color(54, 57, 63));
//            UIManager.put("nimbusLightBackground", new Color(65, 68, 77));
            UIManager.put("nimbusOrange", new Color(191, 98, 4));
            UIManager.put("nimbusRed", new Color(169, 46, 34));
            UIManager.put("nimbusSelection", new Color(32, 35, 37));
            UIManager.put("scrollbar", new Color(32, 35, 37));
            UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
            UIManager.put("nimbusSelectionBackground", new Color(25, 143, 194));
            UIManager.put("text", new Color(255, 255, 255));
//            Painter<TextField> painter = (com.sun.java.swing.Painter<TextField>) (g, object, width, height) -> {
//                g.setBackground(object.getBackground());
//            };
//            UIManager.put("TextField[Disabled].borderPainter", painter);
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            jButtonStart.setBackground(new Color(27, 28, 31));
            jButtonChoose.setBackground(new Color(27, 28, 31));
            jButtonSave.setBackground(new Color(27, 28, 31));
            mTheme.setForeground(new Color(255, 255, 255));
            mHelp.setForeground(new Color(255, 255, 255));
            for (JMenuItem item : items) {
                item.setForeground(new Color(255, 255, 255));
            }
        } catch (Exception e) {
            System.err.println("Exception caught:" + e);
        }
    }
}
