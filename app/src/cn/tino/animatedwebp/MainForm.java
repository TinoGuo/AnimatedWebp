package cn.tino.animatedwebp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * mailTo:guocheng@xuxu.in
 * Created by tino on 2016 七月 01, 23:32.
 */
public class MainForm {
    private JButton chooseButton;
    private JPanel panel1;
    private JButton dealButton;
    private JTextField quality;
    private JTextField framerate;
    private JFileChooser fc = new JFileChooser();
    private static JFrame frame;
    private File chooseFile;
    private static final String encodeWebpCmd = "/usr/local/Cellar/webp/0.5.0/bin/cwebp -q %d %s -o %s";
    private static final String webpMuxCmd = "/usr/local/Cellar/webp/0.5.0/bin/webpmux %s -loop 0 -bgcolor 0,0,0,0 -o %s.webp";
    private static final String singleFileCmd = " -frame %s +%d+0+0+0-b";
    private static final String singleWebpCmd = "/usr/local/Cellar/webp/0.5.0/bin/cwebp -q %d %s -o %s";

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

//        Runtime runtime = Runtime.getRuntime();
//        Process process;
//        try {
//            process = runtime.exec("sudo chmod 777 ./cwebp");
//            process.waitFor();
//            process = runtime.exec("sudo chmod 777 ./webpmux");
//            process.waitFor();
//        } catch (IOException | InterruptedException e) {
//            JOptionPane.showMessageDialog(null, e, "处理结果",
//                    JOptionPane.INFORMATION_MESSAGE);
//            e.printStackTrace();
//        }
    }

    public static void main(String[] args) {
        frame = new JFrame("MainForm");
        frame.setContentPane(new MainForm().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
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

    private void processSinglePic(File file) {
        if (!file.getName().endsWith(".png")) {
            return;
        }
        Runtime runtime = Runtime.getRuntime();
        Process process;
        String name = file.getName().replace(".png", "");
        try {
            process = runtime.exec(String.format(singleWebpCmd, Integer.valueOf(quality.getText()), file.getAbsolutePath(), file.getParent() + "/" + name + ".webp"));
            int exitVal = process.waitFor();
            InputStream inputStream = process.getErrorStream();
            JOptionPane.showMessageDialog(null, exitVal == 0 ? "OK" : "ERROR " + InputStreamTOString(inputStream), "处理结果",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {

        }
    }

    private void processDeal() {
        if (chooseFile != null && chooseFile.isFile()) {
            processSinglePic(chooseFile);
        }
        Runtime runtime = Runtime.getRuntime();
        Process process;
        if (chooseFile != null && chooseFile.isDirectory()) {
            File[] files = chooseFile.listFiles(new ImageFileFilter(".png"));
            for (File file :
                    files) {
                String name = file.getName().replace(".png", "");

                try {
                    process = runtime.exec(String.format(encodeWebpCmd, Integer.valueOf(quality.getText()), file.getAbsolutePath(), file.getParent() + "/" + name + ".webp"));
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
            System.out.println(String.format(webpMuxCmd, sb.toString(), chooseFile.getPath()+ "/"));
            try {
                process = runtime.exec(String.format(webpMuxCmd, sb.toString(), chooseFile.getParent() + "/" + chooseFile.getName()));
                int exitVal = process.waitFor();
                InputStream inputStream = process.getErrorStream();
                JOptionPane.showMessageDialog(null, exitVal == 0 ? "OK" : "ERROR " + InputStreamTOString(inputStream), "处理结果",
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

    public static String InputStreamTOString(InputStream in) throws Exception{

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int count = -1;
        while((count = in.read(data,0,1024)) != -1)
            outStream.write(data, 0, count);

        data = null;
        return new String(outStream.toByteArray(),"ISO-8859-1");
    }

    private class ImageFileFilter implements FileFilter {
        String filer;
        public ImageFileFilter(String filer) {
            this.filer = filer;
        }

        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(filer);
        }
    }
}
