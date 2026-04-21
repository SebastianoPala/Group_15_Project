package com.unipi.PlayerHive.utility;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ArrayPager {

    private final int start;
    private final int limit;

    // this class is used to paginate upside down arrays (the first element is in the last position, and new elements are appended)

    public ArrayPager(int total, int page_number, int page_size){

        int startingReverseIndex = total - page_number * page_size - 1;

        limit = (startingReverseIndex + 1 - page_size < 0) ? startingReverseIndex + 1 : page_size;

        start = startingReverseIndex - page_size + 1;
    }

    public boolean isOutOfBounds(){
        return start < 0;
    }

}
