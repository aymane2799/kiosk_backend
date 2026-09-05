package com.example.kiosk.content;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ContentServiceImplementation implements ContentService{
    private final ContentRepository contentRepository;



}
