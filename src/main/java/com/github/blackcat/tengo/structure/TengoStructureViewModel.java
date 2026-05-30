package com.github.blackcat.tengo.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.github.blackcat.tengo.TengoFile;
import org.jetbrains.annotations.NotNull;

public class TengoStructureViewModel extends StructureViewModelBase
        implements StructureViewModel.ElementInfoProvider {

    public TengoStructureViewModel(@NotNull TengoFile file) {
        super(file, new TengoStructureViewElement(file));
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        Object value = element.getValue();
        return value instanceof com.github.blackcat.tengo.psi.TengoParam
                || value instanceof com.github.blackcat.tengo.psi.TengoForInVar;
    }
}
