package com.example.kiosk.config;

import com.example.kiosk.auth.AppUser;
import com.example.kiosk.auth.AppUserRepository;
import com.example.kiosk.auth.Role;
import com.example.kiosk.content.Content;
import com.example.kiosk.content.ContentRepository;
import com.example.kiosk.content.ContentTier;
import com.example.kiosk.subscription.plan.Plan;
import com.example.kiosk.subscription.plan.PlanRepository;
import com.example.kiosk.tenant.Tenant;
import com.example.kiosk.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevSeeder implements CommandLineRunner {
    private final TenantRepository tenantRepository;
    private final AppUserRepository appUserRepository;
    private final ContentRepository contentRepository;
    private final PlanRepository planRepository;


    @Override
    public void run(String... args) throws Exception {
        if(tenantRepository.count() > 0) {
            return;
        }

        Tenant leMatin = tenantRepository.save(
                Tenant.builder()
                        .name("Le Matin")
                        .slug("le-matin")
                        .logoUrl("https://placehold.co/200x60?text=Le+Matin")
                        .primaryColor("#1E3A8A")
                        .secondaryColor("#FBBF24")
                        .providerId("le-matin-oauth")
                        .build());

        Tenant radarSport = tenantRepository.save(
                Tenant.builder()
                        .name("Radar Sport")
                        .slug("radar-sport")
                        .logoUrl("https://placehold.co/200x60?text=Radar+Sport")
                        .primaryColor("#065F46")
                        .secondaryColor("#F97316")
                        .providerId("radar-sport-oauth")
                        .build()
        );

        appUserRepository.save(
                AppUser.builder()
                        .tenant(leMatin)
                        .externalId("le-matin-user-1")
                        .email("alice@lematin.example")
                        .firstName("Alice")
                        .lastName("Dupont")
                        .role(Role.USER)
                        .build()
        );

        appUserRepository.save(
                AppUser.builder()
                        .tenant(leMatin)
                        .externalId("le-matin-user-2")
                        .email("admin@lematin.example")
                        .firstName("Admin")
                        .lastName("Le Matin")
                        .role(Role.ADMIN)
                        .build()
        );

        appUserRepository.save(
                AppUser.builder()
                        .tenant(radarSport)
                        .externalId("radar-sport-user-1")
                        .email("bob@radar-sport.example")
                        .firstName("Bob")
                        .lastName("Martin")
                        .role(Role.USER)
                        .build()
        );

        Instant now = Instant.now();

        contentRepository.save(
                Content.builder()
                        .title("Les bases du café filtre")
                        .excerpt("Un guide rapide pour réussir son café filtre à la maison.")
                        .body("Contenu factice détaillant la métode pas à pas")
                        .category("Lifestyle")
                        .tier(ContentTier.FREE)
                        .publishedAt(now)
                        .build()
        );

        contentRepository.save(
                Content.builder()
                        .title("Météo de la semaine")
                        .excerpt("Les prévisions météo pour les 7 prochains jours.")
                        .body("Contenu factice météo.")
                        .category("Actualités")
                        .tier(ContentTier.FREE)
                        .publishedAt(now)
                        .build()
        );

        contentRepository.save(
                Content.builder()
                        .title("Analyse tactique : le pressing haut")
                        .excerpt("Comment les meilleures équipes européennes structurent leur pressing.")
                        .body("Contenu factice d'analyse tactique détaillée.")
                        .category("Sport")
                        .tier(ContentTier.PREMIUM)
                        .publishedAt(now)
                        .build()
        );

        contentRepository.save(
                Content.builder()
                        .title("Dossier exclusig : mercato d'hiver")
                        .excerpt("Toutes les rumeurs et transfers du mercato.")
                        .body("Contenu factice exclusif.")
                        .category("Sport")
                        .tier(ContentTier.PREMIUM)
                        .publishedAt(now)
                        .build()
        );

        for (Tenant tenant : List.of(leMatin, radarSport)) {
            planRepository.save(
                    Plan.builder()
                            .tenant(tenant)
                            .name("Essentiel")
                            .tier(ContentTier.FREE)
                            .price(0)
                            .currency("EUR")
                            .billingPeriod(30)
                            .build()
            );

            planRepository.save(
                    Plan.builder()
                            .tenant(tenant)
                            .name("Premium")
                            .tier(ContentTier.PREMIUM)
                            .price(999)
                            .currency("EUR")
                            .billingPeriod(30)
                            .build()
            );
        }
    }

}
