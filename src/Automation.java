import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;

public class Automation {
    private class Node{
        public State current_state = new State();
        public State previous_state = new State();
        public StringBuilder current_word = new StringBuilder();
    }
    private enum States{
        ERROR,
        IDENTIFIER,
        NUMBER,
        OPERATOR,
        COMPOUND_OPERATOR,
        COMMENT,
        DIRECTIVE,

        PUNCTUATOR,
        LITERAL,
        FINAL
    }
    private class State{
        public State() { }
        public State(States state, HashMap<States, Predicate<Character>> predicates){
            current_state = state;
            transitions = predicates;
        }
        private States current_state = States.FINAL;
        private HashMap<States, Predicate<Character>> transitions = new HashMap<>();
        public void SetTransition(States state, Predicate<Character> predicate){
            if(!transitions.containsKey(state)){
                transitions.put(state, predicate);
            } else {
                transitions.replace(state, predicate);
            }
        }
        public void SetState(States state){
            current_state = state;
        }
        public States GetState(){ return current_state; }
        public HashMap<States, Predicate<Character>> GetTransition() { return transitions; }
    }
    private ArrayList<State> states = new ArrayList<>();
    private final HashSet<String> half_operators = Service.newHashSet("=", "-", "*", "|", ">", "<", "&", "%", "!", "~", "^", ":");
    // I should definitely test on the end of string
    private final HashSet<String> punctuators = Service.newHashSet(")", "(", "[", "]", ",", ".", "{", "}", ";", "?", " ", "\n");
    private final HashSet<Character> allowed_letters_in_hex = Service.newHashSet('A', 'B', 'C', 'D', 'E', 'F');

    public Automation() {
        for(States state: States.values()){
            HashMap<States, Predicate<Character>> helper = new HashMap<>();
            switch (state){
                case ERROR -> {
                }
                case IDENTIFIER -> {
                    helper.put(States.IDENTIFIER, c -> {return Character.isLetter(c) || Character.isDigit(c) || c == '_';});
                    helper.put(States.FINAL, c -> {return punctuators.contains(Character.toString(c)) || c == '\\';});
                    helper.put(States.ERROR, c -> {return c == '#' || c == '\'' || c == '"'; });
                }
                case NUMBER -> {
                    helper.put(States.NUMBER, c -> {return Character.isDigit(c) || c == 'x' || allowed_letters_in_hex.contains(c);});
                    helper.put(States.FINAL, c -> {return punctuators.contains(Character.toString(c)) || half_operators.contains(Character.toString(c))
                             || c == '\\';});
                    helper.put(States.ERROR, c -> {return Character.isLetter(c) || c == '#' || c == '\'' || c == '"';});
                }
                case OPERATOR -> {
                    // think over template predicate for
                    //
//                    helper.put(States.FINAL, c -> {
//                        String operator = Character.toString(c).concat()
//                    })
                }
                case COMPOUND_OPERATOR -> {
                    // think over this
                }
                case COMMENT -> {
                    // / /*
                    helper.put(States.COMMENT, c -> { return c == '\\' || c == '*';});

                }
                case DIRECTIVE -> {
                }
                case PUNCTUATOR -> {
                }
                case LITERAL -> {
                }
                case FINAL -> {
                }
            }
        }
        HashMap<States, Predicate<Character>> helper = new HashMap<>();
        states.add(new State(States.IDENTIFIER, c -> { return Character.isLetter(c) || Character.isDigit(c) || c == '_';}));
        states.add(new State(States.NUMBER, Character::isDigit));
        states.add(new State(States.FINAL, c -> {return punctuators.contains(Character.toString(c)) || c == '\'' || c == '"';}));
        states.add(new State(States.OPERATOR, c -> { return half_operators.contains(Character.toString(c));}));

    }
    private ArrayList<Token> tokens = new ArrayList<>();
    private boolean inComment = false;
    private void SetFirstState(char first_char){
        current_word.append(first_char);
        if(Character.isLetter(first_char)){
            current_state = States.IDENTIFIER;
        } else if(Character.isDigit(first_char)){
            current_state = States.NUMBER;
        } else if(half_operators.contains(Character.toString(first_char))){
            current_state = States.OPERATOR;
        } else if(first_char == '\'' || first_char == '"'){
            current_state = States.LITERAL;
        } else if(punctuators.contains(Character.toString(first_char))){
            current_state = States.FINAL;
            tokens.add(new Token(Tokenizer.TokenType.PUNCTUATIONS, Character.toString(first_char)));
        }
    }
    public ArrayList<Token> GetTokensFromString(String line){
        line = line.trim();
        if(line.isEmpty()) return tokens;
        SetFirstState(current_word.charAt(0));
        for(int i = 1; i < line.length(); ++i){
            switch (current_state){

                case ERROR -> {
                    break;
                }
                case IDENTIFIER -> {
                    break;
                }
                case NUMBER -> {
                    break;
                }
                case OPERATOR -> {
                    break;
                }
                case COMPOUND_OPERATOR -> {
                    break;
                }
                case COMMENT -> {
                    break;
                }
                case DIRECTIVE -> {
                    break;
                }
                case LITERAL -> {
                    break;
                }
                case FINAL -> {
                    break;
                }
                default -> {

                }
            }
        }
        return tokens;
    }
}
