package logging

import (
	"context"

	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

const LoggerId = "logger"

func newLogger() *zap.SugaredLogger {
	var config *zap.Config
	config = &zap.Config{
		Level:            zap.NewAtomicLevelAt(zapcore.DebugLevel),
		Development:      true,
		Encoding:         "console",
		EncoderConfig:    developmentEncoderConfig,
		OutputPaths:      []string{"stderr"},
		ErrorOutputPaths: []string{"stderr"},
	}

	logger, err := config.Build()
	if err != nil {
		logger = zap.NewNop()
	}

	return logger.Sugar()
}

func WithLogger(ctx context.Context) context.Context {
	return context.WithValue(ctx, LoggerId, newLogger())
}

func FromContext(ctx context.Context) *zap.SugaredLogger {
	logger, _ := ctx.Value(LoggerId).(*zap.SugaredLogger)
	return logger
}

var developmentEncoderConfig = zapcore.EncoderConfig{
	TimeKey:        "",
	LevelKey:       "L",
	NameKey:        "N",
	CallerKey:      "C",
	FunctionKey:    zapcore.OmitKey,
	MessageKey:     "M",
	StacktraceKey:  "S",
	LineEnding:     zapcore.DefaultLineEnding,
	EncodeLevel:    zapcore.CapitalLevelEncoder,
	EncodeTime:     zapcore.ISO8601TimeEncoder,
	EncodeDuration: zapcore.StringDurationEncoder,
	EncodeCaller:   zapcore.ShortCallerEncoder,
}
