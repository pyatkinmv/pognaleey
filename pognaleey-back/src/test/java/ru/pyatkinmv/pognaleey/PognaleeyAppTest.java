package ru.pyatkinmv.pognaleey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.pyatkinmv.pognaleey.model.TravelInquiry;
import ru.pyatkinmv.pognaleey.repository.TravelInquiryRepository;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class PognaleeyAppTest {

    @Autowired
    private TravelInquiryRepository travelInquiryRepository;

    @Test
    void test() {
        travelInquiryRepository.save(new TravelInquiry(null, "params", Instant.now()));
        var inquiries = travelInquiryRepository.findById(1L);
        assertThat(inquiries).isPresent();
    }
}
