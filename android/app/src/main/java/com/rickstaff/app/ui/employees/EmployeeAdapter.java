package com.rickstaff.app.ui.employees;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.rickstaff.app.R;
import com.rickstaff.app.data.model.Employee;
import java.util.ArrayList;
import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {

    private List<Employee> items = new ArrayList<>();
    private final OnActionListener listener;

    public interface OnActionListener {
        void onEdit(Employee employee);
        void onDelete(Employee employee);
    }

    public EmployeeAdapter(OnActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Employee> newList) {
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return newList.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                return items.get(o).getId() == newList.get(n).getId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                return items.get(o).equals(newList.get(n));
            }
        });
        items = new ArrayList<>(newList);
        diff.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Employee emp = items.get(position);
        holder.bind(emp);
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(emp));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(emp));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvCargo, tvEmail, tvSalario, tvAtivo, tvIniciais;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View v) {
            super(v);
            tvNome = v.findViewById(R.id.tvNome);
            tvCargo = v.findViewById(R.id.tvCargo);
            tvEmail = v.findViewById(R.id.tvEmail);
            tvSalario = v.findViewById(R.id.tvSalario);
            tvAtivo = v.findViewById(R.id.tvAtivo);
            tvIniciais = v.findViewById(R.id.tvIniciais);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }

        void bind(Employee emp) {
            Context context = itemView.getContext();
            String salario = context.getString(R.string.employee_salario)
                    + String.format("%.2f", emp.getSalario());

            tvNome.setText(emp.getNome());
            tvCargo.setText(emp.getCargo());
            tvEmail.setText(emp.getEmail());
            tvSalario.setText(salario);
            tvAtivo.setText(emp.isAtivo() ? R.string.employee_ativo : R.string.employee_inativo);

            String[] parts = emp.getNome().split(" ");
            String iniciais = parts.length >= 2
                ? String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[1].charAt(0))
                : String.valueOf(parts[0].charAt(0));
            tvIniciais.setText(iniciais.toUpperCase());
        }
    }
}