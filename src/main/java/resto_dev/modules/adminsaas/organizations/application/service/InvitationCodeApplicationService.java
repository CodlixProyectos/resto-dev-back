package resto_dev.modules.adminsaas.organizations.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.InvitationCodeJpaEntity;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.InvitationCodeJpaRepository;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.GenerateInvitationCodeRequest;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output.InvitationCodeResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationCodeApplicationService {

    private final InvitationCodeJpaRepository invitationCodeRepository;
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional(readOnly = true)
    public Page<InvitationCodeResponse> getAllCodes(Pageable pageable) {
        return invitationCodeRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public InvitationCodeResponse generateCode(GenerateInvitationCodeRequest request) {
        String code = generateRandomCode();
        
        InvitationCodeJpaEntity entity = InvitationCodeJpaEntity.builder()
                .code(code)
                .planName(request.getPlanName())
                .trialDays(request.getTrialDays())
                .used(false)
                .build();

        entity = invitationCodeRepository.save(entity);
        log.info("🎫 Nuevo código de invitación generado: {} para el plan {}", code, request.getPlanName());
        
        return mapToResponse(entity);
    }

    @Transactional
    public void deleteCode(UUID id) {
        InvitationCodeJpaEntity entity = invitationCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Código no encontrado"));
        
        if (entity.isUsed()) {
            throw new RuntimeException("No se puede eliminar un código que ya ha sido utilizado");
        }

        invitationCodeRepository.delete(entity);
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder("RT-");
        for (int i = 0; i < 6; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private InvitationCodeResponse mapToResponse(InvitationCodeJpaEntity entity) {
        return InvitationCodeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .planName(entity.getPlanName())
                .trialDays(entity.getTrialDays())
                .used(entity.isUsed())
                .createdAt(entity.getCreatedAt())
                .usedAt(entity.getUsedAt())
                .usedByOrganizationName(entity.getUsedByOrganizationName())
                .build();
    }
}
