package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import web.car_system.Car_Service.domain.dto.showroom.CreateShowroomRequest;
import web.car_system.Car_Service.domain.dto.showroom.ShowroomDto;
import web.car_system.Car_Service.domain.dto.showroom.ShowroomReviewDto;
import web.car_system.Car_Service.domain.dto.showroom.UpdateShowroomRequest;
import web.car_system.Car_Service.domain.entity.Showroom;
import web.car_system.Car_Service.domain.entity.ShowroomReview;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShowroomMapper {

    @Mapping(target = "managerId", source = "manager.userId")
    @Mapping(target = "managerName", source = "manager.fullName")
    ShowroomDto toDto(Showroom showroom);

    Showroom toEntity(CreateShowroomRequest request);

    void updateEntityFromRequest(UpdateShowroomRequest request, @MappingTarget Showroom showroom);

    @Mapping(target = "showroomId", source = "showroom.id")
    @Mapping(target = "showroomName", source = "showroom.name")
    @Mapping(target = "customerId", source = "customer.userId")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "customerAvatar", source = "customer.picture")
    @Mapping(target = "replyById", source = "replyBy.userId")
    @Mapping(target = "replyByName", source = "replyBy.fullName")
    ShowroomReviewDto toReviewDto(ShowroomReview review);
}
