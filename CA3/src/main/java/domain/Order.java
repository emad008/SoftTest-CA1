package domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    int id;
    int customer;
    int price;
    int quantity;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Order order) {
            return id == order.id;
        }
        return false;
    }
}
