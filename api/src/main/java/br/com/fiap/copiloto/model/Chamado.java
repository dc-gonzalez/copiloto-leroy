package br.com.fiap.copiloto.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * agoraBanco is the database clock at read time; timestamps come from the database
 * clock too, so elapsed time is right even when the API host runs in another time zone.
 */
public record Chamado(long id, int senha, Long idProjeto, int idCorredor, Long idProduto, String motivo,
                      StatusChamado status, String vendedor, LocalDateTime abertoEm, LocalDateTime aceitoEm,
                      LocalDateTime concluidoEm, LocalDateTime agoraBanco) {

    /** Minutes the customer waited (until accepted, or until now while still open). */
    public long minutosDeEspera() {
        LocalDateTime fim = aceitoEm != null ? aceitoEm : agoraBanco;
        return Math.max(0, Duration.between(abertoEm, fim).toMinutes());
    }
}
