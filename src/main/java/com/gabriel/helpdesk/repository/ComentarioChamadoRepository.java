package com.gabriel.helpdesk.repository;

import com.gabriel.helpdesk.model.ComentarioChamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioChamadoRepository
        extends JpaRepository<ComentarioChamado, Long> {

    List<ComentarioChamado>
    findByChamadoIdOrderByDataHoraAsc(Long chamadoId);
}