package com.gestion.bibliotheque.repositories;

import com.gestion.bibliotheque.entities.EmpruntEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpruntRepository extends JpaRepository<EmpruntEntity, Integer> {

    Optional<EmpruntEntity> findByProcessInstanceKey(Long processInstanceKey);

    boolean existsByLivreNumero(int numero);

    boolean existsByMembreId(int id);
}
