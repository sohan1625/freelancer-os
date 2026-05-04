package com.freelancer.freelanceros.repository;

import com.freelancer.freelanceros.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByWorkspaceId(Long workspaceId);

    void deleteByWorkspaceId(Long workspaceId);

    @Query("SELECT c FROM Client c JOIN FETCH c.workspace WHERE c.id = :id")
    Optional<Client> findByIdWithWorkspace(@Param("id") Long id);

    Optional<Client> findByPortalToken(String portalToken);
}