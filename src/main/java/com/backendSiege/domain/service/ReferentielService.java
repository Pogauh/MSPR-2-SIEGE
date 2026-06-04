package com.backendSiege.domain.service;

import com.backendSiege.domain.model.Exploitation;
import com.backendSiege.domain.port.ReferentielPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ReferentielService {

    private final ReferentielPort referentielPort;

    public List<Exploitation> getAllExploitationsByPays(String pays) {
        return referentielPort.findAllExploitationsByPays(pays);
    }

}
