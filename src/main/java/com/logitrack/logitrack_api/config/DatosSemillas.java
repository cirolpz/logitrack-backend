package com.logitrack.logitrack_api.config;

import com.logitrack.logitrack_api.model.Envio;
import com.logitrack.logitrack_api.model.EstadoEnvio;
import com.logitrack.logitrack_api.repository.EnvioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class DatosSemillas {

    @Bean
    CommandLineRunner initDatabase(EnvioRepository repository) {
        return args -> {

            if(repository.count() == 0){

                Envio e1 = new Envio();
                e1.setTrackingId(UUID.randomUUID().toString());
                e1.setDni("40123456");
                e1.setNombre("Karin");
                e1.setApellido("Pellegrini");
                e1.setDireccion("Av Siempre Viva 742");
                e1.setCodigoPostal("1665");
                e1.setPeso(2.5);
                e1.setEstado(EstadoEnvio.REGISTRADO);

                Envio e2 = new Envio();
                e2.setTrackingId(UUID.randomUUID().toString());
                e2.setDni("38999888");
                e2.setNombre("Ciro");
                e2.setApellido("Lopez");
                e2.setDireccion("Calle Falsa 123");
                e2.setCodigoPostal("1614");
                e2.setPeso(1.2);
                e2.setEstado(EstadoEnvio.EN_TRANSITO);

                Envio e3 = new Envio();
                e3.setTrackingId(UUID.randomUUID().toString());
                e3.setDni("35555111");
                e3.setNombre("Melina");
                e3.setApellido("Scabini");
                e3.setDireccion("San Martin 550");
                e3.setCodigoPostal("2000");
                e3.setPeso(3.0);
                e3.setEstado(EstadoEnvio.EN_SUCURSAL);

                repository.save(e1);
                repository.save(e2);
                repository.save(e3);
            }
        };
    }
}