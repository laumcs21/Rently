
package com.Rently.Business.Service;

import com.Rently.Business.DTO.AlojamientoImagenDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AlojamientoImagenService {

    List<AlojamientoImagenDTO> listar(Long anfitrionId, Long alojamientoId);

    AlojamientoImagenDTO subir(Long anfitrionId, Long alojamientoId, MultipartFile file, Integer orden) throws Exception;

    void borrar(Long anfitrionId, Long alojamientoId, Long imagenId) throws Exception;
}
