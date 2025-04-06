package com.karol.userprograms;

import com.karol.KarolProgram;
import com.karol.Karol;

public class MyProgram implements KarolProgram {
    @Override
    public void run(Karol karol) {
        karol.move();
    }
}
