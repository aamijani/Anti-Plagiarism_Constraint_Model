public class LetterToken extends Token{

    @Override
    public String toString() {
        return stringRepresentation;
    }

    private String stringRepresentation;


    public LetterToken(String stringRepresentation) {
        this.setStringRepresentation(stringRepresentation);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((stringRepresentation == null) ? 0 : stringRepresentation.replaceAll("[^a-zA-Z' ]+", "").toLowerCase().hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LetterToken other = (LetterToken) obj;
        if (stringRepresentation == null) {
            if (other.stringRepresentation != null)
                return false;
        } else if (!stringRepresentation.replaceAll("[^a-zA-Z' ]+", "").toLowerCase().equals(other.stringRepresentation.replaceAll("[^a-zA-Z' ]+", "").toLowerCase()))
            return false;
        return true;
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

    public void setStringRepresentation(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

}
