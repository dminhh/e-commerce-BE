package com.prod.chains;

import com.prod.chains.data.ChainData;
import org.springframework.stereotype.Component;

@Component
public interface ChainHandler<T> {
    Chain<T> handle(ChainData<T> chainData);
}
