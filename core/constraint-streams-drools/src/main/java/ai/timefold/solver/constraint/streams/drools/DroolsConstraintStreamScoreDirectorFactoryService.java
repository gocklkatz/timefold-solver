package ai.timefold.solver.constraint.streams.drools;

import static ai.timefold.solver.core.api.score.stream.ConstraintStreamImplType.BAVET;

import java.util.Objects;
import java.util.function.Supplier;

import ai.timefold.solver.constraint.streams.common.AbstractConstraintStreamScoreDirectorFactory;
import ai.timefold.solver.constraint.streams.common.AbstractConstraintStreamScoreDirectorFactoryService;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.ConstraintStreamImplType;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.util.ConfigUtils;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.score.director.AbstractScoreDirectorFactory;
import ai.timefold.solver.core.impl.score.director.ScoreDirectorType;

public final class DroolsConstraintStreamScoreDirectorFactoryService<Solution_, Score_ extends Score<Score_>>
        extends AbstractConstraintStreamScoreDirectorFactoryService<Solution_, Score_> {

    @Override
    public ScoreDirectorType getSupportedScoreDirectorType() {
        return ScoreDirectorType.CONSTRAINT_STREAMS;
    }

    @Override
    public Supplier<AbstractScoreDirectorFactory<Solution_, Score_>> buildScoreDirectorFactory(ClassLoader classLoader,
            SolutionDescriptor<Solution_> solutionDescriptor, ScoreDirectorFactoryConfig config,
            EnvironmentMode environmentMode) {
        ConstraintStreamImplType constraintStreamImplType_ =
                Objects.requireNonNullElse(config.getConstraintStreamImplType(), ConstraintStreamImplType.BAVET);
        if (constraintStreamImplType_ == BAVET) {
            return null;
        }
        if (config.getConstraintProviderClass() != null) {
            if (!ConstraintProvider.class.isAssignableFrom(config.getConstraintProviderClass())) {
                throw new IllegalArgumentException(
                        "The constraintProviderClass (" + config.getConstraintProviderClass()
                                + ") does not implement " + ConstraintProvider.class.getSimpleName() + ".");
            }
            return () -> {
                ConstraintProvider constraintProvider = ConfigUtils.newInstance(config,
                        "constraintProviderClass", config.getConstraintProviderClass());
                ConfigUtils.applyCustomProperties(constraintProvider, "constraintProviderClass",
                        config.getConstraintProviderCustomProperties(), "constraintProviderCustomProperties");
                boolean isDroolsAlphaNetworkEnabled =
                        Objects.requireNonNullElse(config.isDroolsAlphaNetworkCompilationEnabled(), true);
                if (config.getGizmoKieBaseSupplier() != null) {
                    return new DroolsConstraintStreamScoreDirectorFactory<>(solutionDescriptor,
                            (KieBaseDescriptor<Solution_>) config.getGizmoKieBaseSupplier(),
                            isDroolsAlphaNetworkEnabled);
                }
                return buildScoreDirectorFactory(solutionDescriptor, constraintProvider, environmentMode,
                        isDroolsAlphaNetworkEnabled);
            };
        } else {
            if (config.getConstraintProviderCustomProperties() != null) {
                throw new IllegalStateException("If there is no constraintProviderClass (" + config.getConstraintProviderClass()
                        + "), then there can be no constraintProviderCustomProperties ("
                        + config.getConstraintProviderCustomProperties() + ") either.");
            }
            return null;
        }
    }

    @Override
    public boolean supportsImplType(ConstraintStreamImplType constraintStreamImplType) {
        return constraintStreamImplType == ConstraintStreamImplType.DROOLS;
    }

    @Override
    public AbstractConstraintStreamScoreDirectorFactory<Solution_, Score_> buildScoreDirectorFactory(
            SolutionDescriptor<Solution_> solutionDescriptor, ConstraintProvider constraintProvider,
            EnvironmentMode environmentMode, boolean droolsAlphaNetworkCompilationEnabled) {
        return new DroolsConstraintStreamScoreDirectorFactory<>(solutionDescriptor, constraintProvider,
                droolsAlphaNetworkCompilationEnabled);
    }

    @Override
    public ConstraintFactory buildConstraintFactory(SolutionDescriptor<Solution_> solutionDescriptor,
            EnvironmentMode environmentMode) {
        return new DroolsConstraintFactory<>(solutionDescriptor);
    }

}
