package com.gabriel.helpdesk.repository;

import com.gabriel.helpdesk.model.HistoricoChamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoChamadoRepository extends JpaRepository<HistoricoChamado, Long> {

    List<HistoricoChamado> findByChamadoIdOrderByDataHoraAsc(Long chamadoId);
}