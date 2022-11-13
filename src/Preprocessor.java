import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.Map;
import java.util.Iterator;
public class Preprocessor {
    private static Map<String, String> trigraghs = Map.of(
            "??=", "#",
            "??/", "\\",
            "??'", "^",
            "??(", "[",
            "??)", "]",
            "??!", "|",
            "??<", "{",
            "??>", "}",
            "??-", "~"
    );

    // replace all the trigraphs in the text by its values
    private static Stream<String> replace_trigraphs(Stream<String> text){
        return text.map(word -> {
            String [] line = {word};
            trigraghs.forEach((trg, val) -> line[0] = line[0].replace(trg, val));
            return line[0];
        });
    }
    // delete '/' and make a single line
    private static Stream<String> form_macros(Stream<String> text){
        // here we have to replace all the '/' and make a single-lined macros
        Iterator<String> it = text.iterator();
        StringBuilder macros_to_form = new StringBuilder();
        ArrayList<String> new_stream = new ArrayList<>();
        boolean macros = false;
        while(it.hasNext()){
            String line = it.next();
            if(line.endsWith("\\")){
                macros = true;
                line = line.substring(0, line.length() - 1);
                macros_to_form.append(line);
                continue;
            }
            if(macros){
                macros_to_form.append(line);
                macros = false;
                new_stream.add(macros_to_form.toString());
                macros_to_form.setLength(0);
            } else{
                new_stream.add(line.trim());
            }
        }
        return new_stream.stream();
    }

    static public Stream<String> Preprocess(Stream<String> text){
        return form_macros(replace_trigraphs(text));
    }

}