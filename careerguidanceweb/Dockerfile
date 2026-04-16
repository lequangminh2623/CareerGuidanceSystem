FROM node:20-alpine AS base

FROM base AS builder
WORKDIR /app

# Install dependencies based on package manager
COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
# Environment variables must be present at build time
# If a .env.local file exists, it will act as configuration, but if not we shouldn't fail
# So we use a wildcard or safely copy
COPY .env* ./

# Build Next.js
RUN npm run build

FROM base AS runner
WORKDIR /app

ENV NODE_ENV production

# Create a non-root group and user
RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

# Set correct permissions
RUN mkdir .next
RUN chown nextjs:nodejs .next

# Copy built assets
COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package.json ./package.json
# Ensure env gets carried over if needed at runtime
COPY --from=builder /app/.env* ./

USER nextjs

EXPOSE 3000

ENV PORT 3000
ENV HOSTNAME "0.0.0.0"

CMD ["npm", "start"]
