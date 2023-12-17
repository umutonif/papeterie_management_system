package com.farida.PapeterieManagementSystem.dao;





import com.farida.PapeterieManagementSystem.model.Papeterie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PapeterieRepository extends PagingAndSortingRepository<Papeterie, Long> {
    //searching using a keyword
    @Query("SELECT p FROM Papeterie p WHERE "
            + "CONCAT(p.name,p.description,p.price, p.quantity, p.purchaseDate," +
            " p.category)" + "LIKE %?1%")
    public Page<Papeterie> findAll(String keyword, Pageable pageable);
}
