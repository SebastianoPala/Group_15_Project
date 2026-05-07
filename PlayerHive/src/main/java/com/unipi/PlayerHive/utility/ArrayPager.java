package com.unipi.PlayerHive.utility;

import lombok.Getter;

@Getter
public class ArrayPager {

    private final int total;
    private final int size;

    private final int start;
    private final int limit;


    // this class is used to paginate upside down arrays (the first element is in the last position, and new elements are appended)

    public ArrayPager(int total_elements, int page_number, int page_size){

        total = total_elements;
        size = page_size;

        int startingReverseIndex = total_elements - page_number * page_size - 1;

        limit = (startingReverseIndex + 1 - page_size < 0) ? startingReverseIndex + 1 : page_size;

        start = startingReverseIndex - limit + 1;
    }

    public boolean isOutOfBounds(){
        return start < 0 || limit <= 0;
    }

    public int getNumPages(){
        return (total / size) + ((total % size) != 0 ? 1 : 0);
    }

    // the last element has been requested, further queries will go out of bounds
    public boolean isLastPage(){
        return start == 0;
    }

}
