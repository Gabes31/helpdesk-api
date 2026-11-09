package com.gabriel.helpdesk.repository;

import com.gabriel.helpdesk.model.Chamado;
import com.gabriel.helpdesk.model.StatusChamado;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    List<Chamado> findBySolicitanteIdOrderByIdDesc(Long solicitanteId);
    List<Chamado> findByResponsavelIdOrderByIdDesc(Long responsavelId);
    List<Chamado> findByResponsavelIsNullAndStatusInOrderByIdAsc(List<StatusChamado> status);
}