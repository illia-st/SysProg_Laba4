import javax.swing.plaf.nimbus.State;
import java.util.*;
import java.util.function.Predicate;

public class Automation {
    private class Node{
        public State_Info current_stateInfo;
        public State_Status current_stateStatus;
        public State_Status previous_state;
        public StringBuilder current_word = new StringBuilder();
    }
    private enum State_Status {
        ERROR,
        IDENTIFIER,
        NUMBER,
        OPERATOR,
        COMPOUND_OPERATOR,
        COMMENT,
        DIRECTIVE,
        PUNCTUATOR,
        CHAR_LITERAL,
        STRING_LITERAL,
        FINAL
    }
    private class State_Info {
        public boolean in_hex = false;
        public boolean end_of_error = false;
        public boolean in_comment = false;
        public State_Info() { }
        public State_Info(HashMap<State_Status, Predicate<Character>> predicates){
            transitions = predicates;
        }
        private HashMap<State_Status, Predicate<Character>> transitions = new HashMap<>();
        public void SetTransition(State_Status state, Predicate<Character> predicate){
            if(!transitions.containsKey(state)){
                transitions.put(state, predicate);
            } else {
                transitions.replace(state, predicate);
            }
        }
        public HashMap<State_Status, Predicate<Character>> GetTransition() { return transitions; }
    }
    private HashMap<State_Status, State_Info> states = new HashMap<>();
    private final HashSet<String> half_operators = Service.newHashSet("=", "-", "*", "|", ">", "<", "&", "%", "!", "~", "^", ":");
    // I should definitely test on the end of string
    private final HashSet<String> punctuators = Service.newHashSet(")", "(", "[", "]", ",", ".", "{", "}", ";", "?", " ", "\n");
    private final HashSet<Character> allowed_letters_in_hex = Service.newHashSet('A', 'B', 'C', 'D', 'E', 'F');

    public Automation() {
        for(State_Status state: State_Status.values()){
            State_Info info = new State_Info();
            HashMap<State_Status, State_Info> helper = new HashMap<>();
//            HashMap<State_Status, Predicate<Character>> helper = new HashMap<>();
            switch (state){
                case ERROR -> {
                    info.SetTransition(State_Status.ERROR, c -> {return !punctuators.contains(Character.toString(c));});
                    info.SetTransition(State_Status.FINAL, c -> {return punctuators.contains(Character.toString(c)) || c == '\\';});
                }
                case IDENTIFIER -> {
                    helper.put(State_Status.IDENTIFIER, c -> {return Character.isLetter(c) || Character.isDigit(c) || c == '_';});
                    helper.put(State_Status.FINAL, c -> {return punctuators.contains(Character.toString(c)) ||
                            half_operators.contains(Character.toString(c)) || c == '\\';});
                    helper.put(State_Status.ERROR, c -> {return c == '#' || c == '\'' || c == '"'; });
                }
                case NUMBER -> {
                    helper.put(State_Status.NUMBER, c -> {return Character.isDigit(c) || c == 'x' || allowed_letters_in_hex.contains(c);});
                    helper.put(State_Status.FINAL, c -> {return punctuators.contains(Character.toString(c)) || half_operators.contains(Character.toString(c))
                             || c == '\\';});
                    helper.put(State_Status.ERROR, c -> {return Character.isLetter(c) || c == '#' || c == '\'' || c == '"';});
                }
                case OPERATOR -> {
                    // think over template predicate for
                    helper.put(State_Status.OPERATOR, c -> {return half_operators.contains(Character.toString(c));});
                    helper.put(State_Status.FINAL, c -> {return !half_operators.contains(Character.toString(c));});
                }
                case COMMENT -> {
                    // / /*
                    helper.put(State_Status.COMMENT, c -> { return c == '\\' || c == '*';});
                    helper.put(State_Status.ERROR, c -> {return c != '\\' && c != '*'; });
                }
                case DIRECTIVE -> {
                    helper.put(State_Status.DIRECTIVE, c -> {return Character.isLetterOrDigit(c) || c == '_';});
                    helper.put(State_Status.FINAL, c -> {return punctuators.contains(Character.toString(c)) || c == '\\';});
                    helper.put(State_Status.ERROR, c -> {return !Character.isLetterOrDigit(c) && c != '_' && !punctuators.contains(Character.toString(c));});
                }
                case PUNCTUATOR -> {
                    helper.put(State_Status.FINAL, c->{return punctuators.contains(Character.toString(c));});
                }
                case CHAR_LITERAL -> {
                    helper.put(State_Status.FINAL, c->{return c == '\'';});
                    helper.put(State_Status.CHAR_LITERAL,  c -> {return c != '\'';});
                }
                case STRING_LITERAL -> {
                    helper.put(State_Status.FINAL, c -> {return c == '"';});
                    helper.put(State_Status.STRING_LITERAL,  c -> {return c != '"';});
                }
                case FINAL -> {
                    helper.put(State_Status.CHAR_LITERAL, c -> {return c == '\'';});
                    helper.put(State_Status.STRING_LITERAL, c -> {return c == '"';});
                    helper.put(State_Status.COMMENT, c-> {return c == '\\';});
                    helper.put(State_Status.IDENTIFIER, c -> {return Character.isLetter(c) || c == '_';});
                    helper.put(State_Status.NUMBER, Character::isDigit);
                    helper.put(State_Status.DIRECTIVE, c -> {return c == '#';});
                    helper.put(State_Status.PUNCTUATOR, c -> {return punctuators.contains(Character.toString(c));});
                    helper.put(State_Status.OPERATOR, c -> {return half_operators.contains(Character.toString(c));});
                }
            }
            states.
        }

    }
    private ArrayList<Token> tokens = new ArrayList<>();
    private boolean inComment = false;
    private Node node = new Node();
    private boolean AnaliseHex(){
        for(int i = 0; i < node.current_word.length(); ++i){
            if(i == 0 && node.current_word.charAt(i) != '0') return false;
            if(i == 1 && node.current_word.charAt(i) != 'x') return false;
            if(i > 1 && !Character.isDigit(node.current_word.charAt(i)) && !Tokenizer.allowed_letters_in_hex.contains(node.current_word.charAt(i))){
                return false;
            }
        }
        return true;
    }
    private void SetFirstState(char first_char, String line){
        node.current_word.append(first_char);
        if(Character.isLetter(first_char)){
            // identifier to find
            node.current_stateInfo = states.get(State_Status.IDENTIFIER);
            node.current_stateStatus = State_Status.IDENTIFIER;
        } else if(Character.isDigit(first_char)){
            // number to find
            node.current_stateInfo = states.get(State_Status.NUMBER);
            node.current_stateStatus = State_Status.NUMBER;
            if(first_char == '0'){
                // hex number to find
                node.current_stateInfo.in_hex = true;
            }
        } else if(half_operators.contains(Character.toString(first_char))){
            // operator to find
            node.current_stateInfo = states.get(State_Status.OPERATOR);
            node.current_stateStatus = State_Status.OPERATOR;
        } else if(first_char == '\''){
            // literal to find
            node.current_stateInfo = states.get(State_Status.CHAR_LITERAL);
            node.current_stateStatus = State_Status.CHAR_LITERAL;
        } else if(first_char == '"'){
            node.current_stateInfo = states.get(State_Status.STRING_LITERAL);
            node.current_stateStatus = State_Status.STRING_LITERAL;
        } else if(punctuators.contains(Character.toString(first_char))){
            node.current_stateInfo = states.get(State_Status.FINAL);
            node.current_stateStatus = State_Status.FINAL;
            node.previous_state = State_Status.PUNCTUATOR;
//            tokens.add(new Token(Tokenizer.TokenType.PUNCTUATIONS, Character.toString(first_char)));
        }else if(first_char == '/' && line.length() > 1){
            if(line.charAt(1) == '/'){
                node.current_stateInfo = states.get(State_Status.COMMENT);
            }else if(line.charAt(1) == '*'){
                node.current_stateInfo = states.get(State_Status.COMMENT);
                node.current_stateInfo.in_comment = true;
            }
        }else if(first_char == '#'){
            node.current_stateInfo = states.get(State_Status.DIRECTIVE);
            node.current_stateStatus = State_Status.DIRECTIVE;
        }
        else{
            node.current_stateInfo = states.get(State_Status.ERROR);
        }

    }

    private void GetTokenFromFinalState(){
        // get token
        String token = node.current_word.toString();
        // clear the buffer
        node.current_word.delete(0, node.current_word.length());
        switch (node.previous_state){
            case IDENTIFIER -> {
                if(Tokenizer.keywords.contains(token)){
                    tokens.add(new Token(Tokenizer.TokenType.KEYWORD, token));
                }else{
                    tokens.add(new Token(Tokenizer.TokenType.IDENTIFIER, token));
                }
            }
            case ERROR -> {
                tokens.add(new Token(Tokenizer.TokenType.ERROR, token));
            }
            case OPERATOR -> {
                if(Tokenizer.operators.contains(token)){
                    tokens.add(new Token(Tokenizer.TokenType.OPERATORS, token));
                }else{
                    tokens.add(new Token(Tokenizer.TokenType.ERROR, token));
                }
            }
            case DIRECTIVE -> {
                if(Tokenizer.directives.contains(token)){
                    tokens.add(new Token(Tokenizer.TokenType.PREPROCESSOR_DIRECTIVE, token));
                }else{
                    tokens.add(new Token(Tokenizer.TokenType.ERROR, token));
                }
            }
            case CHAR_LITERAL -> {
                if(token.length() >= 3){
                    if(Tokenizer.special_characters.contains(token)){
                        tokens.add(new Token(Tokenizer.TokenType.CHAR_LITERALS, token));
                    }
                    else{
                        tokens.add(new Token(Tokenizer.TokenType.ERROR, token));
                    }
                }else{
                    tokens.add(new Token(Tokenizer.TokenType.CHAR_LITERALS, token));
                }
            }
            case NUMBER -> {
                tokens.add(new Token(Tokenizer.TokenType.NUMBER_LITERALS, token));
            }
            case STRING_LITERAL -> {
                tokens.add(new Token(Tokenizer.TokenType.STRING_LITERAL, token));
            }
            case PUNCTUATOR -> {
                tokens.add(new Token(Tokenizer.TokenType.PUNCTUATIONS, token));
            }
        }
    }
    public ArrayList<Token> GetTokensFromString(String line){
        tokens.clear();
        line = line.trim();
        if(line.isEmpty()) return tokens;
        SetFirstState(line.charAt(0), line);
        if(node.current_stateStatus == State_Status.COMMENT && !node.current_stateInfo.in_comment){
            return tokens;
        } else if (node.current_stateStatus == State_Status.COMMENT) {
            if(line.contains("*/")){
                node.current_stateInfo.in_comment = false;
                line = line.substring(line.indexOf("*/") + 1);
            }else{
                return tokens;
            }
        }
        boolean comment_start = false, literal = false, number_start = true;
        for(int i = 1; i < line.length(); ++i){
            switch (node.current_stateStatus){
                case ERROR -> {
                    Iterator<Map.Entry<State_Status, Predicate<Character>>> it = node.current_stateInfo.GetTransition().entrySet().iterator();
                    while(it.hasNext()){
                        var value = it.next();
                        if(value.getValue().test(line.charAt(i))){
                            node.previous_state = State_Status.ERROR;
                            node.current_stateInfo = states.get(value.getKey());
                            node.current_stateStatus = value.getKey();
                            break;
                        }
                    }
                }
                case IDENTIFIER -> {
                    Iterator<Map.Entry<State_Status, Predicate<Character>>> it = node.current_stateInfo.GetTransition().entrySet().iterator();
                    while(it.hasNext()) {
                        var value = it.next();
                        // if the identifier is wrong
                        boolean result = value.getValue().test(line.charAt(i));
                        if(result && (value.getKey() == State_Status.ERROR || value.getKey() == State_Status.FINAL)){
                            node.previous_state = State_Status.IDENTIFIER;
                            node.current_stateInfo = states.get(value.getKey());
                            node.current_stateStatus = value.getKey();
                            break;
                        }
                        if(result){
                            node.current_word.append(line.charAt(i));
                            break;
                        }
                    }
                }
                case NUMBER -> {
                    if(number_start){
                        if(line.charAt(i) == '0'){
                            node.current_stateInfo.in_hex = true;
                        }else{
                            node.current_stateInfo.in_hex = false;
                        }
                        number_start = false;
                    }
                    Iterator<Map.Entry<State_Status, Predicate<Character>>> it = node.current_stateInfo.GetTransition().entrySet().iterator();
                    while(it.hasNext()){
                        var value = it.next();
                        // if the identifier is wrong
                        boolean result = value.getValue().test(line.charAt(i));
                        if(result && (value.getKey() == State_Status.ERROR || value.getKey() == State_Status.FINAL)){
                            if(node.current_stateInfo.in_hex && value.getKey() == State_Status.FINAL && !AnaliseHex()){
                                node.previous_state = State_Status.NUMBER;
                                node.current_stateInfo = states.get(State_Status.ERROR);
                                node.current_stateStatus = State_Status.ERROR;
                            }else {
                                node.previous_state = State_Status.NUMBER;
                                node.current_stateInfo = states.get(value.getKey());
                                node.current_stateStatus = value.getKey();
                            }
                            number_start = true;
                            break;
                        }
                        if(result){
                            node.current_word.append(line.charAt(i));
                            break;
                        }
                    }
                }
                case OPERATOR -> {
                    Iterator<Map.Entry<State_Status, Predicate<Character>>> it = node.current_stateInfo.GetTransition().entrySet().iterator();
                    while(it.hasNext()){
                        var value = it.next();
                        boolean result = value.getValue().test(line.charAt(i));
                        if(result && value.getKey() == State_Status.FINAL){
                            node.previous_state = State_Status.OPERATOR;
                            node.current_stateInfo = states.get(value.getKey());
                            node.current_stateStatus = value.getKey();
                            break;
                        }
                        if(result){
                            node.current_word.append(line.charAt(i));
                            break;
                        }
                    }
                }
                case COMMENT -> {
                    if(!comment_start){
                        comment_start = true;
                        node.current_word.append(line.charAt(i));
                        continue;
                    }
                    if(node.current_stateInfo.in_comment){
                        if(line.contains("*/")) {
                            line = line.substring(0, i - 1).concat(line.substring(line.indexOf("*/") + 2));
                            --i;
                            node.previous_state = State_Status.COMMENT;
                            node.current_stateInfo = states.get(State_Status.FINAL);
                            node.current_stateStatus = State_Status.FINAL;
                            comment_start = false;
                            break;
                        }
                        return tokens;
                    }
                    Iterator<Map.Entry<State_Status, Predicate<Character>>> it = node.current_stateInfo.GetTransition().entrySet().iterator();
                    while(it.hasNext()){
                        var value = it.next();
                        boolean result = value.getValue().test(line.charAt(i));
                        if(result && value.getKey() == State_Status.COMMENT){
                            if(line.charAt(i) == '*' && line.contains("*/")){
                                line = line.substring(0, i - 1).concat(line.substring(line.indexOf("*/") + 2));
                                --i;
                                node.previous_state = State_Status.COMMENT;
                                node.current_stateInfo = states.get(State_Status.FINAL);
                                node.current_stateStatus = State_Status.FINAL;
                                comment_start = false;
                                break;
                            } else {
                                node.current_stateInfo.in_comment = true;
                                return tokens;
                            }

                        }else if(result){
                            if(line.charAt(i) != ' '){
                                node.current_word.append(line.charAt(i));
                            }
                            node.previous_state = State_Status.COMMENT;
                            node.current_stateInfo = states.get(State_Status.ERROR);
                            node.current_stateStatus = State_Status.ERROR;
                            break;
                        }
                    }
                }
                case DIRECTIVE -> {
                    Iterator<Map.Entry<State_Status, Predicate<Character>>> it = node.current_stateInfo.GetTransition().entrySet().iterator();
                    while(it.hasNext()){
                        var value = it.next();
                        boolean result = value.getValue().test(line.charAt(i));
                        if(result){
                            if(value.getKey() == State_Status.DIRECTIVE || value.getKey() == State_Status.ERROR){
                                node.current_word.append(line.charAt(i));
                            }
                            if(value.getKey() == State_Status.FINAL || value.getKey() == State_Status.ERROR){
                                node.previous_state = State_Status.DIRECTIVE;
                                node.current_stateInfo = states.get(value.getKey());
                                node.current_stateStatus = value.getKey();
                            }
                            break;
                        }
                    }
                }
                case STRING_LITERAL -> {
                    if(!literal){
                        literal = true;
                        node.current_word.append(line.charAt(i));
                        break;
                    }
                    Iterator<Map.Entry<State_Status, Predicate<Character>>> it = node.current_stateInfo.GetTransition().entrySet().iterator();
                    while(it.hasNext()){
                        var value = it.next();
                        boolean result = value.getValue().test(line.charAt(i));
                        if(result){
                            node.current_word.append(line.charAt(i));
                            if(line.charAt(i) == '"'){
                                node.previous_state = State_Status.STRING_LITERAL;
                                node.current_stateInfo = states.get(value.getKey());
                                node.current_stateStatus = value.getKey();
                                literal = false;
                            }
                            break;
                        }
                    }
                }
                case CHAR_LITERAL -> {
                    if(!literal){
                        literal = true;
                        node.current_word.append(line.charAt(i));
                        break;
                    }
                    Iterator<Map.Entry<State_Status, Predicate<Character>>> it = node.current_stateInfo.GetTransition().entrySet().iterator();
                    while(it.hasNext()){
                        var value = it.next();
                        boolean result = value.getValue().test(line.charAt(i));
                        if(result){
                            node.current_word.append(line.charAt(i));
                            if(line.charAt(i) == '\''){
                                node.previous_state = State_Status.CHAR_LITERAL;
                                node.current_stateInfo = states.get(value.getKey());
                                node.current_stateStatus = value.getKey();
                                literal = false;
                            }
                            break;
                        }
                    }
                }
                case PUNCTUATOR -> {
                    node.current_word.append(line.charAt(i));
                    node.previous_state = State_Status.PUNCTUATOR;
                    node.current_stateInfo = states.get(State_Status.FINAL);
                    node.current_stateStatus = State_Status.FINAL;
                }
                case FINAL -> {
                    GetTokenFromFinalState();
                    --i;
                    while(i < line.length() && Character.isWhitespace(line.charAt(i))){
                        ++i;
                    }
                    if(i == line.length()) break;
                    Iterator<Map.Entry<State_Status, Predicate<Character>>> it = node.current_stateInfo.GetTransition().entrySet().iterator();
                    while(it.hasNext()){
                        var value = it.next();
                        boolean result = value.getValue().test(line.charAt(i));
                        if(result){
                            node.previous_state = State_Status.FINAL;
                            node.current_stateInfo = states.get(value.getKey());
                            node.current_stateStatus = value.getKey();
                            // in the next state we will analise the symbol again
                            --i;
                        }
                    }
                }
            }
        }


        return tokens;
    }
}
