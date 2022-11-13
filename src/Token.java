public class Token{
    public Tokenizer.TokenType type;
    public String value;
    public Token(){}
    public Token(Tokenizer.TokenType token_type, String token_value){
        type = token_type;
        value = token_value;
    }
}