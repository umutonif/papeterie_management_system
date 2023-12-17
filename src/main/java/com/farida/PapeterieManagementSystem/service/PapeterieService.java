package com.farida.PapeterieManagementSystem.service;





import com.farida.PapeterieManagementSystem.model.Papeterie;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PapeterieService {
    Page<Papeterie> findAll(int pageNumber, String keyword);



    Papeterie findById(Long theId);

    void save(Papeterie thePapeterie, Long userId);

    void deleteById(Long theId);

    List<Papeterie> findAll();
}
