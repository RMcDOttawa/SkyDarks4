public enum StartDate {
    NOW("Now"),
    TODAY("Today"),
    GIVEN_DATE("Given");
    private String typeName;

    StartDate(String theTypeName) {
        this.typeName = theTypeName;
    }

    @Override
    public String toString(){
        return this.typeName;
    }
}

