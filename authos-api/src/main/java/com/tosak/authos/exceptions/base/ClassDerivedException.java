package com.tosak.authos.exceptions.base;

public class ClassDerivedException extends Exception {
    public ClassDerivedException() {
    }

    @Override
    public String getMessage() {
        String name = this.getClass().getSimpleName().split("Exception")[0];
        StringBuilder message = new StringBuilder();
        for(int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if(Character.isUpperCase(c)) {
                message.append(" ");
                message.append(Character.toLowerCase(c));
            }else {
                message.append(c);
            }
        }
        return message.toString().trim();
    }
}
