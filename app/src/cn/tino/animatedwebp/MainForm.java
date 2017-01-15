package cn.tino.animatedwebp;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.text.NumberFormat;

/**
 * mailTo:guocheng@xuxu.in
 * Created by tino on 2016 七月 01, 23:32.
 */
public class MainForm {
    private static final String singleFileCmd = " -frame %s +%d+0+0+0-b";
    private static JFrame frame;
    private static String encodeWebpCmd = "%s/cwebp -q %d %s -o %s";
    private static String webpMuxCmd = "%s/webpmux %s -loop %d -bgcolor 0,0,0,0 -o %s.webp";
    private static String singleWebpCmd = "%s/cwebp -q %d %s -o %s";
    private JButton chooseButton;
    private JPanel panel1;
    private JButton dealButton;
    private JTextField quality;
    private JTextField framerate;
    private JRadioButton loopRadioButton;
    private JFormattedTextField loopCountEdit;
    private JFileChooser fc = new JFileChooser();
    private File chooseFile;
    private File localFileDir;

    public MainForm() {
        chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getChooseFile();
            }
        });
        dealButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processDeal();
            }
        });
        loopRadioButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                loopCountEdit.setEditable(e.getStateChange() != ItemEvent.SELECTED);
            }
        });

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setParseIntegerOnly(true);
        final NumberFormatter numberFormatter = new NumberFormatter(numberFormat);
        numberFormatter.setAllowsInvalid(false);
        numberFormatter.setValueClass(Integer.class);
        numberFormatter.setCommitsOnValidEdit(true);
        loopCountEdit.setFormatterFactory(new JFormattedTextField.AbstractFormatterFactory() {
            @Override
            public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
                return numberFormatter;
            }
        });

        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        localFileDir = new File(path);
        if (path.endsWith(".jar")) {
            localFileDir = localFileDir.getParentFile();
        }

    }

    public static void main(String[] args) {
        frame = new JFrame("MainForm");
        frame.setContentPane(new MainForm().panel1);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * output inputStream to String
     *
     * @param in
     * @return human-reading string
     * @throws Exception
     */
    public static String InputStreamTOString(InputStream in) throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int count = -1;
        while ((count = in.read(data, 0, 1024)) != -1)
            outStream.write(data, 0, count);

        data = null;
        return new String(outStream.toByteArray(), "ISO-8859-1");
    }

    private void getChooseFile() {
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(false);
        int select = fc.showOpenDialog(frame);
        if (select == JFileChooser.APPROVE_OPTION) {
            dealButton.setEnabled(true);
            chooseFile = fc.getSelectedFile();
        }
    }

    /**
     * process single file
     *
     * @param file Input file
     */
    private void processSinglePic(File file) {
        if (!file.getName().endsWith(".png")) {
            return;
        }
        Runtime runtime = Runtime.getRuntime();
        Process process;
        String name = file.getName().replace(".png", "");
        try {
            process = runtime.exec(String.format(singleWebpCmd, localFileDir.getAbsolutePath(), Integer.valueOf(quality.getText()), file.getAbsolutePath(), file.getParent() + "/" + name + ".webp"));
            int exitVal = process.waitFor();
            InputStream inputStream = process.getErrorStream();
            JOptionPane.showMessageDialog(null, exitVal == 0 ? "OK" : "ERROR " + InputStreamTOString(inputStream), "处理结果",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {

        }
    }

    /**
     * process selected file
     */
    private void processDeal() {
        if (chooseFile == null) {
            return;
        }
        if (chooseFile.isFile()) {
            processSinglePic(chooseFile);
        } else {
            processFiles();
        }
    }

    /**
     * process multi pics
     */
    private void processFiles() {
        Runtime runtime = Runtime.getRuntime();
        Process process;
        if (chooseFile != null && chooseFile.isDirectory()) {
            File[] files = chooseFile.listFiles(new ImageFileFilter(".png"));
            if (files == null || files.length == 0) {
                return;
            }
            int loopCount = loopRadioButton.isSelected() ? 0 : Integer.parseInt(loopCountEdit.getText());
            for (File file : files) {
                String name = file.getName().replace(".png", "");

                try {
                    process = runtime.exec(String.format(encodeWebpCmd, localFileDir.getAbsolutePath(), Integer.valueOf(quality.getText()), file.getAbsolutePath(), file.getParent() + "/" + name + ".webp"));
                    int exitVal = process.waitFor();
                } catch (IOException |InterruptedException e) {
                    JOptionPane.showMessageDialog(null, e, "处理结果",
                            JOptionPane.INFORMATION_MESSAGE);
                    e.printStackTrace();
                    return;
                }
            }

            StringBuilder sb = new StringBuilder();
            files = chooseFile.listFiles(new ImageFileFilter(".webp"));
            for (File file : files) {
                sb.append(String.format(singleFileCmd, file.getAbsolutePath(), 1000 / Integer.valueOf(framerate.getText())));
            }
            try {
                process = runtime.exec(String.format(webpMuxCmd, localFileDir.getAbsolutePath(), sb.toString(), loopCount, chooseFile.getParent() + "/" + chooseFile.getName()));
                int exitVal = process.waitFor();
                InputStream inputStream = process.getErrorStream();
                JOptionPane.showMessageDialog(null, exitVal == 0 ? "输出路径为" + chooseFile.getAbsolutePath() : "ERROR " + InputStreamTOString(inputStream), "处理结果",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e, "处理结果",
                        JOptionPane.INFORMATION_MESSAGE);
                e.printStackTrace();
            }
            for (File file : files) {
                file.delete();
            }
        }
    }

    /**
     * filter file
     */
    private class ImageFileFilter implements FileFilter {
        final String filer;
        public ImageFileFilter(String filer) {
            this.filer = filer;
        }

        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(filer);
        }
    }
}
