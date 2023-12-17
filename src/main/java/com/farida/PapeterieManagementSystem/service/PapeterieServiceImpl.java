package com.farida.PapeterieManagementSystem.service;





import com.farida.PapeterieManagementSystem.dao.PapeterieRepository;
import com.farida.PapeterieManagementSystem.model.Papeterie;
import com.farida.PapeterieManagementSystem.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Service
public class PapeterieServiceImpl implements PapeterieService {

    private PapeterieRepository papeterieRepository;
    private UserService userService;

    private EntityManager entityManager;

    @Autowired
    public PapeterieServiceImpl(PapeterieRepository thePapeterieRepository, UserService theuserService, EntityManager theentityManager) {
        papeterieRepository = thePapeterieRepository;
        this.userService = theuserService;
        this.entityManager = theentityManager;
    }

    @Override
    public List<Papeterie> findAll() {
        // create query
        TypedQuery<Papeterie> theQuery = entityManager.createQuery("SELECT p FROM Papeterie p ORDER BY p.name ASC", Papeterie.class);

        // return query results
        return theQuery.getResultList();
    }


    // Pageable findAll

    @Override
    public Page<Papeterie> findAll(int pageNumber, String keyword) {
        Sort sort = Sort.by("name").ascending();

        Pageable pageable = PageRequest.of(pageNumber - 1, 2, sort);
        if(keyword !=null){
            return papeterieRepository.findAll(keyword, pageable);
        }
        return papeterieRepository.findAll(pageable);
    }


    @Override
    public Papeterie findById(Long theId) {
        Optional<Papeterie> result = papeterieRepository.findById(theId);

        Papeterie papeterie = null;

        if (result.isPresent()) {
            papeterie = result.get();
        }
        else {
            // we didn't find the project
            throw new RuntimeException("Did not find papeterie id - " + theId);
        }

        return papeterie;
    }


    @Override
    @Transactional
    public void save(Papeterie papeterie, Long UserId) {
        User existingUser = userService.findById(UserId);
        existingUser.add(papeterie);
        papeterieRepository.save(papeterie);
    }

    @Override
    public void deleteById(Long theId) {
        papeterieRepository.deleteById(theId);
    }
}
