package com.backendSiege.infrastructure.client.adapter;

import com.backendSiege.domain.models.Lot;
import com.backendSiege.domain.models.MesureLot;
import com.backendSiege.domain.models.NouveauLot;
import com.backendSiege.domain.models.ResultatLot;
import com.backendSiege.domain.models.ResultatSortieStock;
import com.backendSiege.domain.ports.InventairePort;
import com.backendSiege.infrastructure.client.PaysClients;
import com.backendSiege.infrastructure.client.dto.ChangementStatutPaysDto;
import com.backendSiege.infrastructure.client.dto.ChangementZonePaysDto;
import com.backendSiege.infrastructure.client.dto.LotPaysDto;
import com.backendSiege.infrastructure.client.dto.MesureLotPaysDto;
import com.backendSiege.infrastructure.client.dto.ResultatLotPaysDto;
import com.backendSiege.infrastructure.client.dto.ResultatSortieStockPaysDto;
import com.backendSiege.infrastructure.client.mapper.PaysMapper;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Lots et mouvements de stock lus et écrits dans les backends des pays par leur API REST ({@code /api/lots/...}). */
@Component
public class InventaireAdapter implements InventairePort {

    private final PaysClients paysClients;
    private final PaysMapper mapper;

    public InventaireAdapter(PaysClients paysClients, PaysMapper mapper) {
        this.paysClients = paysClients;
        this.mapper = mapper;
    }

    @Override
    public List<Integer> findQuantitesEnStock(Collection<String> codesPays) {
        return Flux.fromIterable(paysClients.clients(codesPays))
                .flatMapSequential(client -> client
                        .lire(uri -> uri.path("lots/getAllQuantity").build(), Integer.class, "getAllQuantity")
                        .defaultIfEmpty(0))
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    @Override
    public Optional<Lot> findLot(Collection<String> codesPays, UUID idLot) {
        return Flux.fromIterable(paysClients.clients(codesPays))
                .flatMapSequential(client -> client
                        .lire(uri -> uri.path("lots/getLot/{idLot}").build(idLot), LotPaysDto.class, "getLot"))
                .next()
                .map(mapper::toLot)
                .blockOptional();
    }

    @Override
    public List<Lot> findLots(Collection<String> codesPays) {
        return Flux.fromIterable(paysClients.clients(codesPays))
                .flatMapSequential(client -> client
                        .lister(uri -> uri.path("lots/allLotsFromAllEntrepots").build(), LotPaysDto.class, "allLotsFromAllEntrepots"))
                .map(mapper::toLot)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    @Override
    public List<MesureLot> findMesuresDuLot(Collection<String> codesPays, UUID idLot) {
        return Flux.fromIterable(paysClients.clients(codesPays))
                .flatMapSequential(client -> client
                        .lister(uri -> uri.path("lots/getAllMesuresByLotId/{idLot}").build(idLot), MesureLotPaysDto.class,
                                "getAllMesuresByLotId"))
                .map(mapper::toMesureLot)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    @Override
    public List<Lot> findLotsLesPlusAnciens(Collection<String> codesPays) {
        return Flux.fromIterable(paysClients.clients(codesPays))
                .flatMapSequential(client -> client
                        .lire(uri -> uri.path("lots/oldestLot").build(), LotPaysDto.class, "oldestLot"))
                .map(mapper::toLot)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    @Override
    public Optional<ResultatLot> createLot(String codePays, NouveauLot lot) {
        return paysClients.get(codePays)
                .envoyer(HttpMethod.POST, uri -> uri.path("lots").build(), mapper.toDto(lot), ResultatLotPaysDto.class, "createLot")
                .map(mapper::toResultatLot)
                .blockOptional();
    }

    @Override
    public Optional<ResultatLot> updateStatus(String codePays, UUID idLot, String status) {
        return paysClients.get(codePays)
                .envoyer(HttpMethod.PUT, uri -> uri.path("lots/{idLot}/status").build(idLot), new ChangementStatutPaysDto(status),
                        ResultatLotPaysDto.class, "updateStatusLot")
                .map(mapper::toResultatLot)
                .blockOptional();
    }

    @Override
    public Optional<ResultatLot> updateEmplacement(String codePays, UUID idLot, UUID idZone) {
        return paysClients.get(codePays)
                .envoyerOuEchouer(HttpMethod.PUT, uri -> uri.path("lots/{idLot}/zone").build(idLot), new ChangementZonePaysDto(idZone),
                        ResultatLotPaysDto.class)
                .map(mapper::toResultatLot);
    }

    @Override
    public Optional<ResultatSortieStock> stockOut(String codePays, UUID idLot) {
        return paysClients.get(codePays)
                .envoyerOuEchouer(HttpMethod.POST, uri -> uri.path("lots/stock-out").queryParam("lotId", idLot.toString()).build(), null,
                        ResultatSortieStockPaysDto.class)
                .map(mapper::toResultatSortieStock);
    }
}
