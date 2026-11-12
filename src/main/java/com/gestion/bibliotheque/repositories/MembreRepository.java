package com.gestion.bibliotheque.repositories;

import com.gestion.bibliotheque.entities.MembreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembreRepository extends JpaRepository<MembreEntity, Integer> {

}
