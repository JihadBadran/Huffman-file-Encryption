package App;

import Huffman.HuffmanDecoder;
import Huffman.HuffmanEncoder;
import com.sun.javafx.binding.StringFormatter;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("-e")) {
                for (int i = 1; i < args.length; i++) {
                    try {
                        File file = new File(args[i]);

                        // the destination file
                        File outputFile = new File(file.getAbsolutePath() + ".huff");

                        HuffmanEncoder encoder = new HuffmanEncoder(file, outputFile);
                        encoder.encode();

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(e);
                    }
                }
            } else if (args[0].equals("-d")) {
                for (int i = 1; i < args.length; i++) {
                    if (args[i].endsWith(".huff"))
                        try {
                            File file = new File(args[i]);

                            // the destination file
                            File outputFile = new File(file.getParent() + "/" + file.getName().replace(".huff", ""));

                            HuffmanDecoder decoder = new HuffmanDecoder(file, outputFile);
                            decoder.decode();

                            System.out.println();
                            System.out.println();

                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println(e);
                        }
                }
            }

        } else {
            System.out.println("Usage: java App.Main [-e|-d] [file names to encode/decode]");
            System.out.println("-e: encode");
            System.out.println("-d: decode");
        }
    }


    public void encode(ActionEvent actionEvent) {

        try {
            FileChooser fileChooser = new FileChooser();

            File file = fileChooser.showOpenDialog(((Button) actionEvent.getSource()).getParent().getScene().getWindow());
            if (file != null) {
                File out = new File(file.getAbsoluteFile() + ".huff");
                HuffmanEncoder encoder = new HuffmanEncoder(file, out);
                encoder.encode();

                String ratio = StringFormatter.format("%.6f",((float)(file.length() - out.length()) * 100 / file.length())).get();
                String s = "out:" + file.getAbsolutePath() + ".huff\nRatio:" + ratio + "%";

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, s);
                alert.show();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error Writing and Encoding File.");
            alert.show();
            e.printStackTrace();
        }

    }

    public void decode(ActionEvent actionEvent) {

        try {

            FileChooser fileChooser = new FileChooser();
            File source = fileChooser.showOpenDialog(((Button) actionEvent.getSource()).getParent().getScene().getWindow());

            if (source != null && source.getName().endsWith(".huff")) {
                File output = new File(source.getParent() + "/" + source.getName().replace(".huff", ""));
                HuffmanDecoder decoder = new HuffmanDecoder(source, output);
                decoder.decode();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Decoded to " + source.getParent() + "/" + source.getName().replace(".huff", ""));
                alert.show();
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error Reading and Decoding File.");
            alert.show();
            e.printStackTrace();
        }

    }
}
